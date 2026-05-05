import type { ReactNode } from "react";

type PageHeaderProps = {
  eyebrow?: string;
  title: string;
  description: string;
  action?: ReactNode;
};

export function PageHeader({
  eyebrow,
  title,
  description,
  action,
}: PageHeaderProps) {
  return (
    <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
      <div className="max-w-2xl">
        {eyebrow ? (
          <p className="mb-3 text-xs font-semibold uppercase text-[#E85D04]">
            {eyebrow}
          </p>
        ) : null}
        <h1 className="text-3xl font-semibold text-[#F5F5F5] sm:text-4xl">
          {title}
        </h1>
        <p className="mt-3 text-sm leading-6 text-[#A3A3A3] sm:text-base">
          {description}
        </p>
      </div>
      {action ? <div className="shrink-0">{action}</div> : null}
    </div>
  );
}
